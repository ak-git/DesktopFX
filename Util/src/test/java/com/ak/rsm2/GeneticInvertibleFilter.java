package com.ak.rsm2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

public class GeneticInvertibleFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneticInvertibleFilter.class);

  private static final Cache<ProblemInput, Double> FITNESS_CACHE = Caffeine.newBuilder().maximumSize(1 << 16).build();
  private static final LongAdder REAL_EVALUATIONS_COUNTER = new LongAdder();
  private static final LongAdder TOTAL_EVALUATIONS_COUNTER = new LongAdder();

  private GeneticInvertibleFilter() {
  }

  public record ProblemInput(double x, int y, boolean z) {
  }

  /**
   * Модифицированная овражная функция Розенброка с дискретно-логическим рельефом.
   * Глобальный минимум равен 0.0 и достигается при: x = 1.0, y = 1, z = true.
   */
  public static double fitness(ProblemInput input) {
    TOTAL_EVALUATIONS_COUNTER.increment();
    return FITNESS_CACHE.get(input, in -> {
      REAL_EVALUATIONS_COUNTER.increment();
      // Классический овраг Розенброка: f(x,y) = 100 * (y - x^2)^2 + (1 - x)^2
      // Но y у нас дискретный (int), что превращает овраг в каскад ступеней.
      double ravine = 100.0 * StrictMath.pow(in.y() - (in.x() * in.x()), 2) + StrictMath.pow(1.0 - in.x(), 2);

      if (in.z()) {
        // Мир TRUE: Чистый овраг, ведущий к точке (1.0, 1, true), где fitness = 0.0
        return ravine;
      }
      else {
        // Мир FALSE: Овраг искажен синусоидальными ловушками (локальными минимумами).
        // Даже если алгоритм дойдет до центра оврага, он получит штраф и застрянет.
        double traps = 50.0 * (StrictMath.sin(5.0 * in.x()) + 1.0);
        return ravine + traps + 10.0; // +10 гарантирует, что здесь нет глобального минимума
      }
    });
  }

  static void main() {
    FITNESS_CACHE.cleanUp();
    REAL_EVALUATIONS_COUNTER.reset();
    TOTAL_EVALUATIONS_COUNTER.reset();

    InvertibleCodec<ProblemInput, AnyGene<Serializable>> compositeCodec = getCompositeCodec();

    Constraint<AnyGene<Serializable>, Double> codecFiniteConstraint =
        RetryConstraint.of(compositeCodec, input -> Double.isFinite(fitness(input)));

    // 1. Создаем жесткий турнирный селектор для родителей
    Selector<AnyGene<Serializable>, Double> parentSelector = new TournamentSelector<>(5);

    // 2. Создаем чистый EliteSelector на 3 особи для выживших
    Selector<AnyGene<Serializable>, Double> eliteSelector = new EliteSelector<>(3);

    // 3. Создаем турнирный селектор для оставшейся части выживающих особей
    Selector<AnyGene<Serializable>, Double> survivorTournament = new TournamentSelector<>(4);

    // Начальные параметры адаптивности
    double currentMutationRate = 0.12; // Начинаем с агрессивной мутации 12%
    final int totalEpochs = 1 << 3;        // 8 эпох
    final int generationsPerEpoch = 1 << 4; // По 16 поколений в каждой (итого 127 поколений)

    // Хранилище для популяции между перезапусками движка
    EvolutionResult<AnyGene<Serializable>, Double> evolutionState = null;

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      FITNESS_CACHE.cleanUp();
      REAL_EVALUATIONS_COUNTER.reset();

      for (int epoch = 1; epoch <= totalEpochs; epoch++) {
        // 1. Динамически пересобираем движок с актуальной вероятностью мутации
        Engine<AnyGene<Serializable>, Double> engine = Engine.builder(GeneticInvertibleFilter::fitness, compositeCodec)
            .populationSize(1 << 12)
            .optimize(Optimize.MINIMUM)
            .executor(executor)
            .selector(parentSelector)
            .survivorsSelector(eliteSelector)
            .offspringSelector(survivorTournament)
            .alterers(
                new Mutator<>(currentMutationRate), // Переменная скорость мутации
                new SinglePointCrossover<>(0.6)
            )
            .constraint(codecFiniteConstraint)
            .build();

        // 2. Запускаем стрим эволюции
        // Если это не первая эпоха, передаем предыдущее состояние популяции (evolutionState)
        var stream = (evolutionState == null) ? engine.stream() : engine.stream(evolutionState);

        evolutionState = stream
            .limit(generationsPerEpoch) // Крутим строго заданное число поколений для этой эпохи
            .collect(EvolutionResult.toBestEvolutionResult()); // Сохраняем ВСЁ состояние эволюции, а не только фенотип

        // 3. Анализируем промежуточные итоги эпохи
        double bestFitness = evolutionState.bestFitness();
        ProblemInput currentBestInput = compositeCodec.decode(evolutionState.bestPhenotype().genotype());

        LOGGER.atInfo().log("Эпоха {}/{} завершена | Поколение: {} | Мутация: {}% | Лучший фитнес: {} | {}",
            epoch, totalEpochs, evolutionState.generation(), (int) (currentMutationRate * 100),
            String.format("%.6f", bestFitness), currentBestInput.toString());

        // 4. ДИНАМИЧЕСКАЯ АДАПТАЦИЯ
        // Если мы уже близко к дну оврага (фитнес < 5.0) или вышли на плато — снижаем мутацию для точечной подгонки
        if (bestFitness < 5.0 && currentMutationRate > 0.02) {
          currentMutationRate = 0.02;
          LOGGER.atInfo().log(() -> "--> Переход в режим точной оптимизации (Мутация снижена до 2%)");
        }
        else if (bestFitness < 0.1 && currentMutationRate > 0.005) {
          currentMutationRate = 0.005;
          LOGGER.atInfo().log(() -> "--> Режим микро-мутаций (Мутация снижена до 0.5%)");
        }

        if (bestFitness < 1.0E-6) {
          LOGGER.info("--> Идеальный минимум найден досрочно!");
          break;
        }
      }

      ProblemInput bestInput = compositeCodec.decode(evolutionState.bestPhenotype().genotype());
      LOGGER.atInfo().addKeyValue("Невязка", "%.6f".formatted(evolutionState.bestFitness()))
          .addKeyValue("Вычислений", REAL_EVALUATIONS_COUNTER::sum)
          .addKeyValue("Всего попыток", TOTAL_EVALUATIONS_COUNTER::sum)
          .addKeyValue("Экономия за счет глобального кэша", "%.0f%%".formatted((1.0 - REAL_EVALUATIONS_COUNTER.doubleValue() / TOTAL_EVALUATIONS_COUNTER.sum()) * 100))
          .log(bestInput.toString());
    }
  }

  private static InvertibleCodec<ProblemInput, AnyGene<Serializable>> getCompositeCodec() {
    Factory<Genotype<AnyGene<Serializable>>> genotypeFactory = () -> Genotype.of(
        AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(-10.0, 10.0)),
        AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(-21, 21)),
        AnyChromosome.of(() -> ThreadLocalRandom.current().nextBoolean())
    );

    return InvertibleCodec.of(
        genotypeFactory,
        gt -> new ProblemInput(
            (Double) gt.get(0).gene().allele(),
            (Integer) gt.get(1).gene().allele(),
            (Boolean) gt.get(2).gene().allele()
        ),
        input -> Genotype.of(
            AnyChromosome.of(input::x),
            AnyChromosome.of(input::y),
            AnyChromosome.of(input::z)
        )
    );
  }
}





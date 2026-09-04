package com.ak.rsm2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jenetics.*;
import io.jenetics.engine.*;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;

class GeneticTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneticTest.class);

  private static final Cache<ProblemInput, Double> FITNESS_CACHE = Caffeine.newBuilder().maximumSize(1 << 12).build();
  private static final LongAdder REAL_EVALUATIONS_COUNTER = new LongAdder();
  private static final LongAdder TOTAL_EVALUATIONS_COUNTER = new LongAdder();

  @BeforeEach
  void setUp() {
    FITNESS_CACHE.cleanUp();
    REAL_EVALUATIONS_COUNTER.reset();
    TOTAL_EVALUATIONS_COUNTER.reset();
  }

  public record ProblemInput(double x, int y, boolean z) {
  }

  @Disabled("Simple example")
  @RepeatedTest(3)
  void genetic() {
    InvertibleCodec<ProblemInput, AnyGene<Serializable>> compositeCodec = getCompositeCodec();

    Constraint<AnyGene<Serializable>, Double> codecFiniteConstraint =
        RetryConstraint.of(compositeCodec, input -> Double.isFinite(fitness(input)));

    double currentMutationRate = 0.12; // Начинаем с агрессивной мутации 12%
    EvolutionResult<AnyGene<Serializable>, Double> evolutionState = null;

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int epoch = 0, totalEpochs = 1 << 4; epoch < totalEpochs; epoch++) {
        // 1. Динамически пересобираем движок с актуальной вероятностью мутации
        Engine<AnyGene<Serializable>, Double> engine = Engine.builder(GeneticTest::fitness, compositeCodec)
            .populationSize(1 << 12)
            .optimize(Optimize.MINIMUM)
            .executor(executor)
            .selector(new TournamentSelector<>(5)) // жесткий турнирный селектор для родителей
            .survivorsSelector(new EliteSelector<>(3)) // элитный селектор для выживших
            .offspringSelector(new TournamentSelector<>(4)) // турнирный селектор для оставшейся части выживающих особей
            .alterers(new Mutator<>(currentMutationRate), new SinglePointCrossover<>(0.6))
            .constraint(codecFiniteConstraint)
            .build();

        // 2. Запускаем стрим эволюции
        var stream = (evolutionState == null) ? engine.stream() : engine.stream(evolutionState);

        evolutionState = stream.limit(1 << 4) // // По 16 поколений в каждой из 16 эпох (итого 128 поколений)
            .collect(EvolutionResult.toBestEvolutionResult()); // Сохраняем ВСЁ состояние эволюции, а не только фенотип

        // 3. Анализируем промежуточные итоги эпохи
        double bestFitness = evolutionState.bestFitness();
        ProblemInput currentBestInput = compositeCodec.decode(evolutionState.bestPhenotype().genotype());

        LOGGER.atInfo().log("Эпоха {}/{} завершена | Поколение: {} | Мутация: {}% | Лучший фитнес: {} | {}",
            epoch + 1, totalEpochs, evolutionState.generation(), (int) (currentMutationRate * 100),
            String.format("%.6f", bestFitness), currentBestInput);

        // 4. ДИНАМИЧЕСКАЯ АДАПТАЦИЯ
        if (bestFitness < 5.0 && currentMutationRate > 0.02) {
          currentMutationRate = 0.02;
          LOGGER.atInfo().log(() -> "--> Переход в режим точной оптимизации (Мутация снижена до 2%)");
        }
        else if (bestFitness < 0.1 && currentMutationRate > 0.005) {
          currentMutationRate = 0.005;
          LOGGER.atInfo().log(() -> "--> Режим микро-мутаций (Мутация снижена до 0.5%)");
        }

        if (bestFitness < 1.0E-6) {
          LOGGER.atInfo().log(() -> "--> Минимум найден досрочно!");
          break;
        }
      }

      ProblemInput bestInput = compositeCodec.decode(evolutionState.bestPhenotype().genotype());
      LOGGER.atInfo().addKeyValue("Невязка", "%.6f".formatted(evolutionState.bestFitness()))
          .addKeyValue("Вычислений", REAL_EVALUATIONS_COUNTER::sum)
          .addKeyValue("Всего попыток", TOTAL_EVALUATIONS_COUNTER::sum)
          .addKeyValue("Экономия за счет глобального кэша", "%.0f%%".formatted((1.0 - REAL_EVALUATIONS_COUNTER.doubleValue() / TOTAL_EVALUATIONS_COUNTER.sum()) * 100))
          .log(bestInput.toString());

      SoftAssertions.assertSoftly(a -> {
        a.assertThat(bestInput.x).isCloseTo(1.0, Percentage.withPercentage(10));
        a.assertThat(bestInput.y).isCloseTo(1, Percentage.withPercentage(10));
        a.assertThat(bestInput.z).isTrue();
      });
    }
  }

  private static InvertibleCodec<ProblemInput, AnyGene<Serializable>> getCompositeCodec() {
    return InvertibleCodec.of(
        () -> Genotype.of(
            AnyChromosome.of(() -> ThreadLocalRandom.current().nextDouble(-10.0, 10.0)),
            AnyChromosome.of(() -> ThreadLocalRandom.current().nextInt(-21, 21)),
            AnyChromosome.of(() -> ThreadLocalRandom.current().nextBoolean())
        ),
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

  /**
   * Модифицированная овражная функция Розенброка с дискретно-логическим рельефом.
   * Глобальный минимум равен 0.0 и достигается при: x = 1.0, y = 1, z = true.
   */
  private static double fitness(ProblemInput input) {
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
}
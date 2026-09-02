package com.ak.rsm2;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticInvertibleFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(GeneticInvertibleFilter.class);

  private GeneticInvertibleFilter() {
  }

  public record ProblemInput(double x, int y, boolean z) {
  }

  /**
   * Модифицированная овражная функция Розенброка с дискретно-логическим рельефом.
   * Глобальный минимум равен 0.0 и достигается при: x = 1.0, y = 1, z = true.
   */
  public static double fitness(ProblemInput input) {
    double x = input.x();
    double y = input.y();
    boolean z = input.z();

    // Классический овраг Розенброка: f(x,y) = 100 * (y - x^2)^2 + (1 - x)^2
    // Но y у нас дискретный (int), что превращает овраг в каскад ступеней.
    double rosenbrockRavine = 100.0 * StrictMath.pow(y - (x * x), 2) + StrictMath.pow(1.0 - x, 2);

    if (z) {
      // Мир TRUE: Чистый овраг, ведущий к точке (1.0, 1, true), где fitness = 0.0
      return rosenbrockRavine;
    }
    else {
      // Мир FALSE: Овраг искажен синусоидальными ловушками (локальными минимумами).
      // Даже если алгоритм дойдет до центра оврага, он получит штраф и застрянет.
      double traps = 50.0 * (StrictMath.sin(5.0 * x) + 1.0);
      return rosenbrockRavine + traps + 10.0; // +10 гарантирует, что здесь нет глобального минимума
    }
  }

  static void main() {
    InvertibleCodec<ProblemInput, AnyGene<Serializable>> compositeCodec = getCompositeCodec();

    Constraint<AnyGene<Serializable>, Double> codecFiniteConstraint = RetryConstraint.of(compositeCodec, input -> Double.isFinite(fitness(input)));

    // 1. Создаем жесткий турнирный селектор для родителей
    Selector<AnyGene<Serializable>, Double> parentSelector = new TournamentSelector<>(5);

    // 2. ИСПРАВЛЕНИЕ: Создаем чистый EliteSelector на 3 особи для выживших
    Selector<AnyGene<Serializable>, Double> eliteSelector = new EliteSelector<>(3);

    // 3. Создаем турнирный селектор для оставшейся части выживающих особей
    Selector<AnyGene<Serializable>, Double> survivorTournament = new TournamentSelector<>(4);

    // 1. Создаем пул виртуальных потоков Java 26
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Engine<AnyGene<Serializable>, Double> engine = Engine.builder(GeneticInvertibleFilter::fitness, compositeCodec)
          .populationSize(1024)
          .optimize(Optimize.MINIMUM)
          .executor(executor)
          // 4. Подключаем селекторы правильно через методы билдера Jenetics
          .selector(parentSelector)             // Кто становится родителями для скрещивания
          .survivorsSelector(eliteSelector)       // Элита гарантированно выживает
          .offspringSelector(survivorTournament)  // Все остальные потомки отбираются через турнир
          .alterers(
              new Mutator<>(0.08),
              new SinglePointCrossover<>(0.6)
          )
          .constraint(codecFiniteConstraint)
          .build();

      Phenotype<AnyGene<Serializable>, Double> best = engine.stream()
          .limit(Limits.bySteadyFitness(7))
          .limit(100)
          .peek(result -> {
            long generation = result.generation();
            if (generation % 2 == 0) {
              double bestFitness = result.bestFitness();
              ProblemInput currentBestInput = compositeCodec.decode(result.bestPhenotype().genotype());
              LOGGER.info("Эпоха: {} | Лучшая невязка: {} | {}", generation, String.format("%.6f", bestFitness), currentBestInput);
            }
          })
          .collect(EvolutionResult.toBestPhenotype());

      ProblemInput bestInput = compositeCodec.decode(best.genotype());
      LOGGER.atInfo().addKeyValue("fitness", best.fitness()).log(bestInput.toString());
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





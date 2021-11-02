# Knevo

[
![license: MIT](https://img.shields.io/github/license/TimerErTim/Knevo?color=blue&style=flat-square)
](https://github.com/TimerErTim/Knevo/blob/master/LICENSE)
[
![GitHub Workflow Status](
https://img.shields.io/github/workflow/status/TimerErTim/Knevo/Check?style=flat-square
)
](https://github.com/TimerErTim/Knevo/actions/workflows/check.yml)
[
![Sonatype Nexus (Releases)](
https://img.shields.io/nexus/r/eu.timerertim.knevo/knevo?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square
)
](https://search.maven.org/artifact/eu.timerertim.knevo/knevo)
![Maintenance](https://img.shields.io/maintenance/yes/2021?style=flat-square)

A neuroevolution library for Java and Kotlin written purely in Kotlin, featuring multiple neuroevolution algorithms,
serialization and multi-threading.

## Table of Contents

1. [Installation](#installation)
2. [Content](#content)
3. [Usage](#usage)
4. [License](#license)
5. [Project State](#project-state)

## Installation

From Maven Central:

Maven

```xml
<dependencies>
    <dependency>
        <groupId>eu.timerertim.knevo</groupId>
        <artifactId>knevo</artifactId>
        <version>0.1.0-RC2</version>
    </dependency>
</dependencies>
```

Gradle Groovy

```groovy
dependencies {
    implementation 'eu.timerertim.knevo:knevo:0.1.0-RC2'
}
```

Gradle KTS

```kotlin
dependencies {
    implementation("eu.timerertim.knevo:knevo:0.1.0-RC2")
}
```

## Content

Algorithms:

- [x] [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6)
- [ ] [NEAT](http://nn.cs.utexas.edu/keyword?stanley:ec02)
- [ ] [SUNA](https://paperswithcode.com/paper/spectrum-diverse-neuroevolution-with-unified)
- [ ] [AGENT]()

Features:

- Serialization
- Multithreading using Coroutines
- Custom Activation Functions

## Usage

### Environment

You must implement the `Environment` interface and override the `evaluateFitness(population: List<Genome>)` function.

For each `Genome` in the population, you want to set its fitness.

### Instances

Everything requires an Instance. It is the interface to an algorithm implementation.

```kotlin
val instance = InstinctInstanceBuilder(2, 1)
    .mutateAddSelfConnectionChance(0F)
    .mutateAddRecurrentConnectionChance(0F)
    .mutateRemoveConnectionChance(2.15F)
    .hiddenActivations(Sigmoid(), Tanh(), Step(), Sign(), Linear(), Sinus(), Relu(), Selu(), Silu())
    .outputActivations(Sign())
    .build()
```

```kotlin
val network = instance.Network()
println(network(floatArrayOf(0F, 1F)))
```

The Instance can be set globally, so you don't have to explicitly specify the Instance everytime.

```kotlin
globalInstinctInstance = InstinctInstance(2, 1)
```

```kotlin
val network = Network()
println(network(floatArrayOf(0F, 1F)))
```

### Training

You can use individual `Networks` like above. However, you typically want to make use of a bigger population.

```kotlin
val pool = Pool(
    populationSize = 500,
    select = Tournament(10)
)
```

You can use this and some environment to write a training loop.

```kotlin
do {
    pool.evolve(environment)
} while(pool.topFitness < 100)
```

Finally, the trained `Genome` can be retrieved.

```kotlin
val network = pool.topGenome
```

### Coroutines

This library supports multithreading by using Coroutines. `Populations` are separated into batches, which are processed
in parallel. This behavior can be configured on every `Population`.

```kotlin
val pool = PoolBuilder()
    .populationSize(200)
    .batchSize(50)
    .build()
```
Note that when using this feature, the `Environment` has to be thread safe as multiple `Genomes` are evaluated in
parallel.

### Serialization

You can easily save any `Network` or `Pool`:

```kotlin
network.save("out/my_trained_network.knv")
```

And load it as easily:

```kotlin
val network = InstinctNetwork.load("out/my_trained_network.knv") ?: Network()
```

Because the load function returns null if the specified file does not yet exist, you can easily define an
initiative `Network` or `Pool`.

## License

Knevo is licensed under the [MIT License](LICENSE).

## Project State

I, the project owner, am currently attending
the [higher technical college of Grieskirchen](https://github.com/HTBLA-Grieskirchen). My education takes me about as
much time as a full time job, not counting homework and similar tasks. Because of this, the development pace is not as
fast as one may expect. Nonetheless, I like to extend this library as demanded. Just be aware that development might be
or become a little slow.

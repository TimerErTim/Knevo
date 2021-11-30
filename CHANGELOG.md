# [Knevo] Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

Note that this is a full changelog. Changelog relative to the last pre-release can be found in the end.

### Added

- Activation function: Sigmoid, Tanh, Sinus, Step, Sign, Random, ReLU, SeLU, SiLU, Linear.
- Selection functions: Power, Tournament, Fitness Proportionate.
- [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) neuroevolution algorithm.
- Environment interface.
- Fitness function interface.
- Checkpoint manager.

### Relative to [0.2.0-RC](https://github.com/TimerErTim/Knevo/releases/tag/v0.2.0-RC)

## [0.2.0-RC] - 2021-11-30

Note that this is a full changelog. Changelog relative to the last pre-release can be found in the end.

### Added

- Activation function: Sigmoid, Tanh, Sinus, Step, Sign, Random, ReLU, SeLU, SiLU, Linear.
- Selection functions: Power, Tournament, Fitness Proportionate.
- [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) neuroevolution algorithm.
- Environment interface.
- Fitness function interface.
- Checkpoint manager.

### Relative to [0.1.0-RC2](https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-RC2)

#### Added

- Fitness function interface.
- Checkpoint manager.

#### Changed

- Upgraded Kotlin to version `1.6.0` from `1.5.30`.
- Serialization: Saving and Loading are now extensions functions of Serializable objects instead of member functions.
- Environment `evaluateFitness` function is now a suspending function.

### Fixed

- Bug which allowed dead nodes and thus the removal of required connections in
  [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) networks.

## [0.1.0-RC2] - 2021-10-21

Note that this is a full changelog. Changelog relative to the last pre-release can be found in the end.

### Added

- Activation function: Sigmoid, Tanh, Sinus, Step, Sign, Random, ReLU, SeLU, SiLU, Linear.
- Selection functions: Power, Tournament, Fitness Proportionate.
- [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) neuroevolution algorithm.
- Environment interface.

### Relative to [0.1.0-RC1](https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-RC1)

#### Fixed

- Bug which allowed an Instinct neuron to have multiple connections to itself.

## [0.1.0-RC1] - 2021-10-19

Note that this is a full changelog. Changelog relative to the last pre-release can be found in the end.

### Added

- Activation function: Sigmoid, Tanh, Sinus, Step, Sign, Random, ReLU, SeLU, SiLU, Linear.
- Selection functions: Power, Tournament, Fitness Proportionate.
- [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) neuroevolution algorithm.
- Environment interface.

### Relative to [0.1.0-BETA](https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-BETA)

#### Fixed

- Bug which caused a crash if an Instinct network had more outputs than inputs.

## [0.1.0-BETA] - 2021-10-19

### Added

- Activation function: Sigmoid, Tanh, Sinus, Step, Sign, Random, ReLU, SeLU, SiLU, Linear.
- Selection functions: Power, Tournament, Fitness Proportionate.
- [Instinct](https://towardsdatascience.com/neuro-evolution-on-steroids-82bd14ddc2f6) neuroevolution algorithm.
- Environment interface.

[Unreleased]: https://github.com/TimerErTim/Knevo/commits/HEAD

[0.2.0-RC]: https://github.com/TimerErTim/Knevo/releases/tag/v0.2.0-RC

[0.1.0-RC2]: https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-RC2

[0.1.0-RC1]: https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-RC1

[0.1.0-BETA]: https://github.com/TimerErTim/Knevo/releases/tag/v0.1.0-BETA

[0.0.2]: https://github.com/olivierlacan/keep-a-changelog/compare/v0.0.1...v0.0.2

[0.0.1]: https://github.com/olivierlacan/keep-a-changelog/releases/tag/v0.0.1

[Knevo]: https://github.com/TimerErTim/Knevo/

[Unreleased-Example]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.0.0...HEAD

# evo-NEAT
A Kotlin implementation of NEAT(NeuroEvolution of Augmenting Topologies ) for the generation of evolving artificial neural networks with Coroutines support.

Implimentation of http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf

### Usage
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.Advice-Dog:evo-NEAT:-SNAPSHOT'
	}

## Implementation
You must implement the `Environment` interface and override the `evaluateFitness(population: List<Genome>)` function.

For each `Genome` in the population, you want to call `Genome.evaluateNetwork(...)` with your inputs, and then calculate the fitness from the result and set it on the `Genome`.


### Config
You can config how the model is created using the `NEATConfig.Builder`.

    val config: NeatConfig = NEATConfig.Builder()
        .setPopulationSize(300)
        .setBatchSize(100)
        .setInputs(2)
        .setOutputs(1)
        .build()
        
### Example
A full example of the XOR implementation is given in the folder evo-NEAT/src/examples/  .

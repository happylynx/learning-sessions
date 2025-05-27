# Gatherers

## Summary

`Stream.gather(Gatherer)` is a new general intermediate Stream operation.

## Stream recap

* builder-like API
  * It creates a data-processing pipeline. The pipeline can't be referenced.
* `Stream` instances can't be reused 

### Intermediate & terminal operations

* intermediate - return `Stream`
  * e.g. `flatMap()`, `takeWhile()`, `filter()`
* terminal - usually triggers the computation and returns result of the executed pipeline
  * e.g. `forEach()`, `toList()`, `collect()`
  * exceptions: `iterator()`, `spliterator()`

### Parallel streams

* `parallel()` / `sequential()`, last setting matter [Demo](src/main/java/org/example/Parallel_LastSettingMatters.java)
* Processed by the main thread and threads from the `ForkJoinPool.commonPool()`
  [Demo](src/main/java/org/example/ParallelPipeline.java)
  [Demo](src/main/java/org/example/ParallelPipelineWithCustomCollector.java)

![Parallel pipeline](images/parallel-pipeline.drawio.svg)

### Non-interfering, stateless operations

* Non-interference
  * execution of the pipeline lasts when the terminal operation is running
  * during that time the stream source shouldn't be changed, unless its streams
    are [CONCURRENT](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Spliterator.html#CONCURRENT)
* Stateless
  * Result of stateless lambda solely depends on its arguments

### `flatMap()`, `mapMulti()`

```java
<R> Stream<R> map(Function<? super T, ? extends R> mapper);
<R> Stream<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper);
```

* `mapMulti()`
  * no need to create return `Stream` object 
  * imperative approach

[Demo](src/main/java/org/example/FlatMapMapMulti.java)

### Lazy evaluation, short-curcuiting operations

* streams are evaluate lazily, as data would be requested at the end of the pipeline
  and the demand is propagated towards the beginning of the pipeline
  * It allows to work with infinite streams 
* An operation is "short-curcuiting" when it can stop the stream evaluation
  without draining the source.

[Demo](src/main/java/org/example/LazyEvaluation.java)

### Execution elision

* `count()`

[Demo](src/main/java/org/example/OperationExecutionOmitted.java)

### Stream, Spliterator, Iterator

Spliterator - in principle an iterator with `characteristics()`, `estimateSize()` and `trySplit()`

#### Iterator to Stream

```java
Iterator<Integer> iterator = ...;
Stream<Integer> = StreamSupport.stream(Spliterators.spliterator(iterator), false /*parallel*/);
```

#### Stream to Iterator

* `Stream.iterator()`
* `Stream.spliterator()`
* the only terminal operations that allow to gradually read stream results

### Primitive types specialization

* Specialization
  * IntStream
  * LongStream
  * DoubleStream
* Primitives vs References in Java, Generics
* `gather()` not added to the primitive specializations

### Universal terminal operation - `collect()`

* Interface `Collector<T, A, R>`
  * `supplier()`
  * `accumulator()`
  * `combiner()`
  * `finisher()`
  * `characteristics()`

#### Creation

// TODO

#### `Collectors`

// TODO show example of `reduce()` implemented using collector

<details>
<summary>Task</summary>

#### String summary collector
</details>

<details>
<summary>Task</summary>

#### Sliding window average
</details>

## Universal intermediate operation - `gather()`

TODO motivation
* universal operation - less need for further change Stream API
* last element "callback"
* shared state among processing of multiple elements
* control over Sequential / Parallel processing
* ability to "short-circuit" the stream

### `Gatherer<>`

#### `Gatherer<>`

* initializer
* `Integrator`
  * `Downstream`
* combiner
* finisher

#### Steps of evaluation

Sequential evaluation. Combiner is not used.

From [Gatherer](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Gatherer.html) javadoc:

```java
Gatherer.Downstream<? super R> downstream = ...;
A state = gatherer.initializer().get();
for (T t : data) {
    gatherer.integrator().integrate(state, t, downstream);
}
gatherer.finisher().accept(state, downstream);
```

TODO map() reimplementaiton demo
TODO sorted() reimplementation demo

#### Parallel evaluation

* controlled by usage of `Gatherer#defaultCombiner`
  * when used, only evaluated sequentially
  * `Gatherer.of()` vs `Gatherer.ofSequential()`

TODO add a diagram

[Parallel evaluation](src/main/java/org/example/GathererParallelPipeline.java)

#### Short-curcuiting

* integrator returns `false`

#### `andThen()` composition

TODO +1, map-toString demo

#### `Gatherer.Integrator.Greedy`

* `Gatherer.Integrator.ofGreedy`
* optimization of parallel processing

#### Gatherer as a class

[Demo](src/main/java/org/example/AverageGatherer.java)

### `Gatherers`

* `fold()`
* `scan()`
* `windowFixed()`
* `windowsSliding()`
* `mapConcurrent()`

<details>
<summary>Task</summary>

#### Sliding window average using `gather()`
</details>


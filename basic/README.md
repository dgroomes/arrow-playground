# basic

A runnable "hello world" program featuring Apache Arrow.


## Instructions

Follow these instructions to build and run the example program.

1. Use Java 25
2. Build and run the example program
    * ```shell
      ./gradlew run
      ```
    * The program output will look something like this:
    * ```text
      [main] INFO dgroomes.Runner - Let's learn about Apache Arrow!
      [main] INFO org.apache.arrow.memory.BaseAllocator - Debug mode disabled.
      [main] INFO org.apache.arrow.memory.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
      [main] INFO org.apache.arrow.memory.CheckAllocator - Using DefaultAllocationManager at memory-netty/10.0.1/8975307e2967474540cbc8080869767000aee1f7/arrow-memory-netty-10.0.1.jar!/org/apache/arrow/memory/DefaultAllocationManagerFactory.class
      [main] INFO dgroomes.Runner - ZIP code vector: [90210, 19106, 82190]
      [main] INFO dgroomes.Runner - Population vector: [20700, 7043, 443]
      [main] INFO dgroomes.Runner - The highest population is 20700 in ZIP code 90210
      ```

# basic

A runnable "hello world" program featuring Apache Arrow.


## Instructions

Follow these instructions to build and run the example program.

1. Use Java 25
2. Build the program distribution
    - ```shell
      ./gradlew installDist
      ```
3. Run the program
    - ```shell
      build/install/basic/bin/basic
      ```
    - The program output will look something like this:
    - ```text
      [main] INFO app - Let's learn about Apache Arrow!
      [main] INFO org.apache.arrow.memory.BaseAllocator - Debug mode disabled. Enable with the VM option -Darrow.memory.debug.allocator=true.
      [main] INFO org.apache.arrow.memory.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
      [main] INFO org.apache.arrow.memory.CheckAllocator - Using DefaultAllocationManager at memory-netty-18.3.0.jar!/org/apache/arrow/memory/netty/DefaultAllocationManagerFactory.class
      [main] INFO app - ZIP code vector: [90210, 19106, 82190]
      [main] INFO app - Population vector: [20700, 7043, 443]
      [main] INFO app - The highest population is 20700 in ZIP code 90210
      ```

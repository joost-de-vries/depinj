depinj
======

Code that follows the 2012 dependency injection presentation of Runar Bjarnason<sup>1</sup>.
 The code has been a little bit enhanced to serve as an illustration for my talk for Scala Amsterdam [Slick 3.0: functional programming and db side effects](http://www.meetup.com/amsterdam-scala/events/222068171/?comment_table_id=448842564&comment_table_name=event_comment)
 
- The code in package `ex1` shows a first implementation of a `flatmappable` `withConnection` block. 
- The code in `ex2` implements the `>>` 'andThen' combinator and lets `DbAction` _extend_ the function `Connection => A` instead of wrapping it.
- The code in `ex3` abstracts over the type of `Connection` and introduces a type for the Reader monad.

Currently the code doesn't run because of an H2 db driver issue in the code.

<sup>1</sup> 2012 talk for NEScala  
[slides](http://dl.dropboxusercontent.com/u/4588997/Runar-NEScala-DI.pdf)  
[video](https://www.youtube.com/watch?v=ZasXwtTRkio)  


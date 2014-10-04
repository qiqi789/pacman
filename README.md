pacman
======

This project was forked from https://github.com/ScalaPino/pacman. The game's description can be found out on the original project page.

I adapted the project's source to make it runnable against Scala 2.10. Also the Scala actors in the project have been replaced by Akka actors.  

I had to break the original MVC design during the actor migration, because the controller actor(implemented in Akka's) was lift to a top level class (or a static inner class inside a object).

The game now can run in Scala 2.10 with Akka actors. 



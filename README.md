<h2>Gape digging bot using the Baritone API</h2>

Needs lots of refactoring but works well.


For users:

Make a folder in your .minecraft called Digger and place a file called databaseIP in it with the ip of your mongo database, username and password. In that folder, also make a file called partitions with the range of partitions that the player/bot will use. Example: 1 20

Make sure there is only one document in the partitions collection containing the # of partitions and the X and Y coords  of their southeast corner every line. Every partition is 4 chunks x 4 chunks (64x64 blocks).

 -- MongoDB inital layout --

![Screen Shot 2022-08-24 at 3 46 39 AM](https://user-images.githubusercontent.com/20301420/186349616-e65ec626-7827-4574-982c-10425ac54f4d.png)

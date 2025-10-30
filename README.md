# URL shortener service:
# Possible improvements:
* The short url creation and lookup can be split in different services:
  * No need for lookup to be down if creation is unavailable
* In production, we might have to set up database sharding since the amount of data we'll have to store will likely not fit on a single db instance 


Microsoft have a proper Open Source copy and you should probably use that instead: https://github.com/officedev/ews-java-api



Just a few notes.

It needs JDK 1.6. The original documentation says that it works with Exchange 2010
but it seems to work against Exchange 2007 Service Pack 1.

From the website:

Hello EWS Java experts. We have posted an updated EWS Java API package. 
Please note that this package has new list of pre-requisities:
 - Apache Commons HttpClient 3.1
 - Apache Commons Codec 1.4
 - Apache Commons Logging 1.1.1
 - JCIFS 1.3.15.
We made these changes to address issues with incorrect credential caching for 
some types of multi-threaded client implementations caused by a bug 
(http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6626700) with java.net.HttpURLConnection 
(http://download.oracle.com/javase/6/docs/api/java/net/HttpURLConnection.html). 
Thanks for your patience and please let us know if you run into any problems.

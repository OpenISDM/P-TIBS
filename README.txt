There are simple introduction of each code and it's running device.

(1)IS code:

   IShub: It's the tomcat war code running to maintain pub/sub data.
   ISPublishData: It's publish the refuge data to subscriber.

(2)POS(Raspberry Pi) code:

   TomcatRunningOnPi: It's the tomcat war code running to receive the subscribe data.
   HaggleRunningOnPi: It's the code running upon haggle to maintain the pub/sub on POS(Raspberry Pi).

(3)Mobile(Android) code:

   RefugeReader: It's the android app source code let user to receive subscribe data and share receive data to another mobile device.

(4)Trace code:

   TIBSTrace: It's running to connect to IS and POS database to query the tag content and document information, then show the trace result to Gephi.
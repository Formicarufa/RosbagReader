# RosbagReader
## Description
<p>
A simple Java parser of <a href="http://wiki.ros.org/Bags">ROS bag 2.0</a> files written for my <a href="https://github.com/Formicarufa/DroneFlightInspector/">DroneFlightInspector</a>.
</p>
<p>
It can only be used to read the content of the messages. Metadata (chunk and connection records) are skipped and there is no support for compression. The user also has to write the code for reading the content of the messages. More high-level methods such as readString or readFloatArray are available, though.
</p>
## Usage

The interface `RosbagMessageDataParser` has to be implemented. It contains 1 method `parseMessageData` which is called whenever the parser gets to some message. 


```Java
RosbagReader r = new RosbagReader(inputStream);
r.parseBag(new RosbagMessageDataParser() {
       @Override
        public void parseMessageData(RosMessageData rmd) throws IOException, UnexpectedEndOfRosbagFileException {
            if ("topicName".equals(rmd.getTopic()) {
                RosStandardMessageHeader header = rmd.readMessageHeader();
                long time = header.stamp.getTimeAsNanos();
                float val = rmd.readFloat();
                //...
            }
            //You do not have to parse all topics 
            //the reader will skip the rmd.getBytesLeft() bytes
        }
});
```


# Homework 3

### Ankit Kumar Singh
### 651288872
### Email: asing200@uic.edu


## Overview

As part of the homework create a client program that uses gRPC to invoke a lambda function deployed on AWS to determine if the desired timestamp is in the log file. Similarly creat a client program and the corresponding lambda function that use the REST methods (e.g., GET or POST) to interact. For given input of time stamp and time interval the lambda should determine if the log files in some bucket with log messages contain messages in the given time interval from the designated input time stamp and return success code from these messages or some 400-level HTTP client response to designate that log files do not contain any messages in the given time interval.

Note: Please go through the document in the Doc directory to find the detailed explaination and the structure of the project and also how to deploy different component.

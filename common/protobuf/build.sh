#!/bin/sh

protoc -I=./ --java_out=../src/main/java/ ./sampledata.proto
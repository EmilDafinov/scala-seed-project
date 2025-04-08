echo "Image: [$IMAGE]"
sbt 'set version := "1.0"' 'Docker / publishLocal'
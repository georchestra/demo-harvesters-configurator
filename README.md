# About

This Groovy maven project gets the sources from the official geOrchestra website,
parses the markdown file which lists the known geOrchestra instances,
then tries to detect a GeoNetwork CSW endpoint.

If a CSW server is detected, then it checks if a harvesting endpoint already
exists for the instance onto `demo.georchestra.org` and if not, it creates
the endpoint, then launches the harvester.

This project has been written during the `Pre Geocom barcamp 2023` in Rennes.

# How to use

```bash
$ ./mvnw clean package exec:java
```


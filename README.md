# Burp JSON Unicode-Escaper

Burp Extender providing JSON Unicode-escaping/unescaping capabilities. These include:
* Intruder payload processors for escaping (key characters and all characters) and unescaping
* On-demand via context menu items.

This extender was developed using the Burp Montoya API. The extender works with both Burp Suite Professional and Burp Suite Community.

## Usage

1. Load the extender jar in Burp Suite.
2. Configure an Intruder attack and select the appropriate extension payload processor.

## Building

Requires OpenJDK 17+ and Gradle 8+. Other versions of OpenJDK and Gradle have not been tested.

1. Clone the Burp_JSON_Unicode-Escaper repository.
2. Open a terminal and navigate to the Burp_JSON_Unicode-Escaper directory.
3. Issue the following command to compile the extension and create the extension jar file (named ```Burp_JSON_Unicode-Escaper-VERSION.jar```): ```gradle fatJar```

## Copyright

Copyright (C) 2024 Jeffrey Cap (Bort_Millipede)


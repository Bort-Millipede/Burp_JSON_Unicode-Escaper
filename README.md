# Burp JSON Unicode-Escaper

Burp Extender providing JSON Unicode-escaping/unescaping capabilities. These include:
* **Intruder payload processors:** For escaping (key characters or all characters) and unescaping payloads during Intruder attacks.
* **Context menu items:** For escaping (key characters or all characters) and unescaping text in requests/responses on-demand.
  * Within regular editors (ex. Repeater request, Proxy intercepted response), hightlighted text is replaced by the escaped/unescaped text.
  * Within read-only editors (ex. proxy history), the escaped/unescaped text is displayed in a pop-up window

The extender works with both Burp Suite Professional and Burp Suite Community. This extender was developed using the Burp Montoya API, and therefore can only be used on versions of Burp that include this.

## Usage

1. Load the extender jar in Burp Suite.
2. Configure an Intruder attack and select the appropriate extension payload processor.

## Building

Requires OpenJDK 17+ and Gradle 8+. Lesser versions of OpenJDK and Gradle have not been tested.

1. Clone the Burp_JSON_Unicode-Escaper repository.
2. Open a terminal and navigate to the Burp_JSON_Unicode-Escaper directory.
3. Issue the following command to compile the extension and create the extension jar file (named ```Burp_JSON_Unicode-Escaper-VERSION.jar```): ```gradle fatJar```

## Copyright

Copyright (C) 2024 Jeffrey Cap (Bort_Millipede)


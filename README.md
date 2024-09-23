# Burp JSON Unicode-Escaper

Burp Extender providing JSON Unicode-escaping/unescaping capabilities. These include:
* **Intruder payload processors:** For escaping (key characters or all characters) and unescaping payloads during Intruder attacks.
* **Context menu items:** For escaping (key characters or all characters) and unescaping text in requests/responses on-demand.
  * Within regular editors (ex. Repeater request, Proxy intercepted response), hightlighted text is replaced by the escaped/unescaped text.
  * Within read-only editors (ex. proxy history), the escaped/unescaped text is displayed in a pop-up window

The extender works with both Burp Suite Professional and Burp Suite Community. This extender was developed using the Burp Montoya API, and therefore can only be used on versions of Burp that include this.

## Usage

### Loading the Extender

Load the ```Burp_JSON_Unicode-Escaper-VERSION.jar``` extender jar file into Burp Suite with type **Java**.

### Intruder Attacks

Configure an Intruder attack and select the appropriate extension payload processor:

* ```JSON-unescape```: Unescape any escaped characters (such as \n or \u0022) in payload. This processor mostly ignores any unescaped characters. If invalid escape sequences are detected (ex. \y, \u0h00, \u000), the processor will fail and the original unmodified payload will be used.
* ```JSON-escape key chars```: Only escape JSON "key" characters in payload, which includes:
  * control characters (0x00 - 0x1f)
  * double-quotes
  * backslashes
  * some high-ASCII characters (ex. 0x7f)  
  "Dedicated" escape sequences are used for characters that have them (ex. \" for double-quote, \n for newline, etc.).
* ```JSON Unicode-escape key chars```: Only Unicode-escape JSON "key" characters in payload, which includes:
  * control characters (0x00 - 0x1f)
  * double-quotes
  * backslashes
  * some high-ASCII characters (ex. 0x7f)  
  All appropriate characters will be escaped using Unicode-escaping (ex. \u0022 for double-quote, \u000a for newline, etc.).
* ```JSON Unicode-escape all chars```: Unicode-escape all characters in payload (using \uXXXX format, where XXXX is the Unicode hexadecimal value for the character).
* ```JSON Unicode-escape custom chars```**(NOT FULLY IMPLEMENTED YET)**:  Unicode-escape only specific characters in payload (using \uXXXX format, where XXXX is the Unicode hexadecimal value for the character). The characters that should be escaped are defined in the extender's global settings. An option is also available to automatically include the JSON "key" characters in the list of characters to be Unicode-escaped.

#### Important Note on using payloads containing Unicode text in Intruder Attacks

It appears that "Paste" button (for pasting text from the clipboard) and the "Enter a new item" entry field in the "Payloads" tab of Intruder Attacks do not properly handle the entry of Unicode text. These methods appear to interpret the pasted/entered text as ASCII, and therefore do not interpret the full 16-bit Unicode value. Therefore, payloads containing Unicode text that are added to an attack using the button/field will not be correctly added and will therefore be incorrectly interpreted by the extender. 

On the contrary, the "Load ..." button for loading payload(s) from a file appears to handle Unicode text entry correct. Therefore, it is recommended that any payloads containing Unicode text be saved to a file and added to the attack by loading the file.

## Building

Requires OpenJDK 17+ and Gradle 8+. Lesser versions of OpenJDK and Gradle have not been tested.

1. Clone the Burp_JSON_Unicode-Escaper repository.
2. Open a terminal and navigate to the Burp_JSON_Unicode-Escaper directory.
3. Issue the following command to compile the extension and create the extension jar file (named ```Burp_JSON_Unicode-Escaper-VERSION.jar```): ```gradle fatJar```

## Copyright

Copyright (C) 2024 Jeffrey Cap (Bort_Millipede)


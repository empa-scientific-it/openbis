Barcodes
====
 

[](# "Print this article")

 



## Printers

There are several manufacturers of printers and different kinds of
barcodes and paper to adapt to different use cases. Most manufacturers
have their own proprietary printer driver and language for labels.

To allow freedom of choice for the barcode printer, the openBIS ELN-LIMS
allows to configure both the type of barcodes and the layout and size of
the labels. openBIS uses this information to produce a PDF document,
thus having as single requirement that the printer driver used allows to
print PDF documents using applications such as Adobe Acrobat Reader or
Preview (Mac).

 

### Printer Configuration

There are different types of printer drivers. The two types we can
define as generic are **PS** (PostScript) (recommended) and **PCL**
(Printer Command Language). Printers with these drivers are likely to
print PDF documents and other types of documents with embedded fonts,
images, etc…

The printer paper type needs to be configured for each printer. Two
layouts are supported:

-   **Split**: The PDF will contain separate pages with each barcode.
-   **Continuous**: The PDF will contain a continuous layout with the
    barcodes. More uncommon for this applications.

The printer paper size needs to be configured for each printer. It is
possible to indicate the size of the barcode, so it can fit.

 

### Printer testing

We provide two example documents that can be used to test the printer.

 

-   Split barcodes example PDF:
    [printer-test-code128-split-50-15](https://openbis.ch/wp-content/uploads/2021/08/printer-test-code128-split-50-15.pdf)
-   Continuous barcodes example PDF:
    [printer-test-code128-continuous-50-15](https://openbis.ch/wp-content/uploads/2021/08/printer-test-code128-continuous-50-15.pdf)

 

Please consider that these examples likely do not correspond to the
particular paper size of the printer being evaluated and as such the
barcodes may look squashed. In order to obtain optimal results, the
paper size would need to be configured. However, for the test it is
enough to verify that the printer can print those files.

 



### Printer Advice before purchasing

Before purchasing a printer, we recommend to check with the manufacturer
that the barcode printer provides a general driver and that it can print
one of the documents provided as example above.

 

### Tested Printers

-   Zebra ZD420

 


## Scanners

There are several manufacturers of barcode scanners. In most cases
scanners act as a keyboard for the computer, so when the barcode scanner
scans a barcode it will type whatever has been scanned.

 

### Scanner Configuration

The scanner keyboard layout should be the same as the computer used. If
not this could cause problems if there are any special characters.

 

### Scanner testing

Open a notepad and scan the barcodes provided in the examples below. The
scanner should read them and type the correct output.

 

-   Barcode Code 128.
    [scanner-test-code128-50-15](https://openbis.ch/wp-content/uploads/2021/08/scanner-test-code128-50-15.pdf).
    This should give as output “20210720122856003-454071” without
    quotes.
-   Barcode QR Code.
    [scanner-test-qrcode-50-50](https://openbis.ch/wp-content/uploads/2021/08/scanner-test-qrcode-50-50.pdf).
    This should give as output “20210720122856003-454071” without
    quotes.
-   Barcode Micro QR Code.
    [scanner-test-microqrcode-30-30](https://openbis.ch/wp-content/uploads/2021/08/scanner-test-microqrcode-30-30.pdf).
    This should give as output “20210720122856003-454071” without
    quotes.

 


### Scanner Advice before purchasing

Before purchasing a scanner, ensure that the barcode scanner provides a
keyboard driver and ask the manufacturer’s support to scan the examples
above.

 

### Tested Scanners

-   Honeywell 1902G-BF

Updated on July 27, 2022

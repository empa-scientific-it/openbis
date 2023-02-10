#!/bin/bash
#URL to the server without the dash '/'
serverURL=http://wishart.biology.ualberta.ca
#Path to input fasta file
fastaInputFile=FRP1955.fasta
#Path to output svg file
svgOutputFile=FRP1955.svg
#Path to output html file
htmlOutputFile=FRP1955.html

echo Asking service $serverURL/PlasMapper/servlet/DrawVectorMap to generate the SVG file
imageURL=$(curl -k -F "fastaFile=@$fastaInputFile;type=application/octet-stream" \
-F "vendor=Amersham%20Pharmacia" \
-F "Submit=Graphic Map" \
-F "showOption=1" \
-F "showOption=2" \
-F "showOption=3" \
-F "showOption=4" \
-F "showOption=5" \
-F "showOption=6" \
-F "showOption=7" \
-F "showOption=8" \
-F "showOption=9" \
-F "restriction=1" \
-F "orfLen=200" \
-F "strand=1" \
-F "strand=2" \
-F "dir1=1" \
-F "dir2=1" \
-F "dir3=1" \
-F "dir4=1" \
-F "dir5=1" \
-F "dir6=1" \
-F "category1=origin_of_replication" \
-F "category2=origin_of_replication" \
-F "category3=origin_of_replication" \
-F "category4=origin_of_replication" \
-F "category5=origin_of_replication" \
-F "category6=origin_of_replication" \
-F "scheme=0" \
-F "shading=0" \
-F "labColor=0" \
-F "labelBox=1" \
-F "labels=0" \
-F "innerLabels=0" \
-F "legend=0" \
-F "arrow=0" \
-F "tickMark=0" \
-F "comment=Created using PlasMapper" \
-F "imageFormat=SVG" \
-F "imageSize=1000 x 1000" \
-F "backbone=medium" \
-F "arc=medium" \
-F "biomoby=true" \
$serverURL/PlasMapper/servlet/DrawVectorMap
)
echo Downloading SVG file: $serverURL/$imageURL
curl -o $svgOutputFile $serverURL/$imageURL
echo Generating HTML file: $htmlOutputFile
echo "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\"><title>PlasMapper - Graphic Map</title></head><body><embed src=\"$svgOutputFile\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" id=\"Panel\" height=\"1010\" width=\"1010\"><br><a href=\"$svgOutputFile\" target=\"_blank\">Download Link</a></body></html>" > $htmlOutputFile
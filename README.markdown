# XWeb wiki
XWeb wiki module. It's support mediawiki format now.


=XWeb wiki=
XWeb wiki module. You can easily load and coverts wiki data from the server side.

== Setup wiki module ==
You need to create a directory called 'wiki' in resource directory if data folder, then copy all the wiki files to this directory as text file. 
for example sample_test.txt

You can load wiki HTML file like this:
<pre>
/api?api=wiki&html=sample_test
</pre>

== Features ==
All the Wiki that cach on the hard driver and it will update automaticlly when you change the wiki text file
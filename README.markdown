# XWeb wiki
XWeb wiki module. It support mediawiki format now.

## How to use it?
Simple put module definition in xweb.xml

```xml
<module>
	<name>wiki</name>
	<author>Hamed Abdollahpour</author>
	<class>ir.xweb.module.WikiModule</class>
	<validators>
		<!-- TODO: restrict illegal value to cheat system -->
		<validator param="edit" regex=".*?" />
		<validator param="html" regex=".*?" />
		<validator param="image" regex=".*?" />
	</validators>
	<roles>
    <!-- Administrator can do anything - If you have different role for administration change this item -->
		<role definite="true" param="" eval="true" value="admin" />
		<!-- Everyone can get informations -->
		<role param="get" eval="true" value=".*?" />
	</roles>
</module>
```


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

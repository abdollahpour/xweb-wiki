# XWeb wiki
XWeb wiki module. It support mediawiki format now.

## How to use it?
Simple put module definition in xweb.xml

```xml
<module>
	<name>wiki</name>
	<author>Hamed Abdollahpour</author>
	<class>ir.xweb.module.WikiModule</class>
	<roles>
		<!-- Administrator can do anything - If you have different role for administration change this item -->
		<role definite="true" param="" eval="true" value="admin" />
		<!-- Everyone can get informations -->
		<role param="get" eval="true" value=".*?" />
	</roles>
	<properties>
		<property ket='dir.wiki'></property>
		<property ket='dir.cache'></property>
	</properties>
</module>
```

## Validation
You do not need to validate for parametters, it will handle by module itself.

## Requirements
You need to add resource modules first.

## Properties
**dir.wiki: ** directory that contain wiki contents. Ex:
```
- start.mediawiki
- sample_content.mediawiki
- sample.png
- picture.jpg
...
```

**dir.wiki: ** directory that we store wiki data (It's not optional). Ex:
```xml
<!-- wiki directory, it's that 'wiki' folder in ROOT in your website -->
<property ket='dir.wiki'>${xweb.root}/wiki</property>
```

**dir.cache: ** Directory that generated HTML file and the other things (It's optional). If you don't set this option, it will be same as dir.wiki


XWeb wiki module. You can easily load and coverts wiki data from the server side.

## How can we use it with HTML
### Get wiki data
We can simply get HTML content of wiki files:
```javascript
// request for start.mediawiki
/api?api=wiki&get=start
```

### Put data with upload file
You can also put image and wiki files (now just mediawiki)
```html
<form action='/api?api=wiki' enctype='multipart/form-data'>
	<input type='file' name='file1' />
	<input type='file' name='file2' />
	<input type='submit' value='send it now' />
</form>
```

### Put data with post
```html
<form action='/api?api=wiki'>
	<input type='text' name='title' />
	<textarea name='put'></textarea>
	<input type='submit' value='send it now' />
</form>
```

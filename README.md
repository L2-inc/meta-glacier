## Meta-Glacier

What distinguishes this AWS Glacier software from other similar software is its
integration with the free online-metadata storage API.  When your archives are
uploaded to the AWS Glacier, the archive metadata can be optionally saved to
your metadata account.  You can later search this database to select what to
download.

If you just want to download a working software, download the zip file
including all third party libraries [here](http://repo.vrane.com/downloads). 
Then on a Mac, it's a matter of unzipping and double-clicking the first jar
file.  On Windows, you first have to download [Java](http://www.java.com/en/) 
and then the procedure is the same as Mac.

### Features

#### Metadata API features

- save and search photo metadata
- save gps coordinates and search by location keywords
- search by keywords in the archive descriptions
- search by a range of archive sizes
- search by a range of upload dates
- search by a range of file modification dates
- search by a range of gps dates
- save archive contents by file paths
- free

## Other features

- zipping multiple files before uploading as one archive
- multi-part upload; progress bar indicates your large uploads
- move files to a specfied folder after the upload
- securely save your credentials
- remember most settings
- search interface to metadata API

### Compatibilities

The software is developed and tested with java 7 on both Mac OS 10.7 and
Windows 7. It is also tested to be working on linux.

You can also use this software to manage the vaults and archives uploaded using
a different software. Naturally, the metadata will not be available for such
vaults and archives but your basic vault and archive data will be stored in
your account.

### Requirements and Dependencies

To compile the source code, you will need the following open source java
libraries. 

[AWS java sdk](http://aws.amazon.com/sdkforjava/) is required to access your
AWS glacier account.

[commons-logging](http://commons.apache.org/proper/commons-logging/) is
required by AWS sdk.

[meta-glacier-sdk](https://github.com/amherst-robots/meta-glacier-sdk) is
required for accessing your metadata account.

[datechooser](https://github.com/amherst-robots/dateChooser) is used by the
search interface for searching date ranges.

[Encryption](https://gihub.com/amherst-robots/encryption) is used to protect
your saved secret credentials on your computer should you choose to save them.

[Mainframe](https://github.com/amherst-robots/mainFrame) is also used to manage
the main JFrame.

These four jar files can be downloaded at

	http://repo.vrane.com/downloads/java

[Jackson JSON Processor](http://jackson.codehaus.org/) is used by metadata sdk to convert
between Java and JSON data structures.  The jar files used are
jackson-mapper-asl-1.9.11 and jackson-core-asl-1.9.11

[commons-codec](http://commons.apache.org/proper/commons-codec) is required by
metadata sdk to compute file checksums.

[metadata-extractor](http://code.google.com/p/metadata-extractor) is used by
metadata sdk to extract photo metadata

[xmpcore](http://mvnrepository.com/artifact/com.adobe.xmp/xmpcore/)
is required by the previous 'metadata-extractor' library.

[commons-validator](http://commons.apache.org/proper/commons-validator) is used
for the validation of email addresses.

[springutilities](http://docs.oracle.com/javase/tutorial/uiswing/examples/layout/SpringGridProject/src/layout/SpringUtilities.java)
is used in the programming of Java UI.

[Apache HttpComponents](http://hc.apache.org/) is used to process http
requests.  The following jar files are used to compile: httpclient-4.2.2 and httpcore-4.2.2

### Setting up a NetBeans project

If you have a github account, use the following command on Mac and linux to get
the latest version of the source code

	git clone git@github.com:amherst-robots/meta-glacier.git

This will create a folder named `meta-glacier`.

Alternatively, from within the NetBeans IDE 7.1 or later, choose `Team` ->
`Git` -> `Clone`.  Use `github.com:amherst-robots/meta-glacier.git` as
"Repository URL" with `git` as "Username".  When prompted to open the netbeans
project, select "No" because the source code does not contain
`project.properties` file and must be created using the instructions below.

If you do not have a github account, replace the urls above with

	https://github.com/amherst-robots/meta-glacier-sdk.git

Inside the sub-folder `nbproject`, copy `netbeans.properties` to
`project.properties`.  Then, from inside [NetBeans](http://www.netbeans.org)
IDE open `meta-glacier` folder.  Next, in an expanded "project" view,
right-click "Libraries" folder and add the downloaded libraries mentioned
above.

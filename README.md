java_mega_api
=====

mega.co.nz API for java or android.

WORK IN PROGRESS!

To test this code get an api key from https://mega.co.nz/#sdk and
run the maven command "mvn -DMEGA_API_KEY=your_key test" to open a command line shell.

To step through some of the existing functionality you can use the following commands:

<ol>
<li>"login [username] [password]"</li>
<li>"get [top level file name]" (downloads a top level file into a directory)</li>
<li>"whoami" (prints session information)</li>
</ol>

So you can basically login and download a file.
The session is persisted in a megatest_session.json file in the current directory,
(as well as your command line history in a megatest_history.txt file)







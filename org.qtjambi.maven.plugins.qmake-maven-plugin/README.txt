
Testing:
	Need to allow user to set:
		* Intended 32bit/64bit/auto-detect kind
			This should check for known problems with configuration on known platforms.
			Windows: %PATH% has the wrong JDK/JRE kind in it somewhere, auto-remote it
			Windows: Compiler auto-detect
			Windows: Qt SDK auto-detect

	Check envvar
		%CLASSPATH%  (unset it if detected, warning about it, should not be needed, allow user option to keep it)

	We should probably work on the basis of filtering out the environment
	 and providing options to selectively allow through things.
	
	Make it run "moc.exe -v" and "rcc.exe -v" for self-test.  This can show up issues with lack of full DLLs (zlib1.dll/*ssl*.dll)


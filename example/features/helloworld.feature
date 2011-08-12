Feature: Hello World Feature
	In order to ensure that my installation works
	As a Developer
	I want to run a quick Cuke4Duke test
	
	@hello @world
	Scenario: Hello World Scenario
		Given the Action is Hello
		When the Subject is World
		Then the Greeting is Hello, World
	
	@goodbye @world
	Scenario Outline: Goodbye World Scenario
		Given the Action is <action>
		When the Subject is <subject>
		Then the Greeting is <action>, <subject>
		
		Examples:
		| action  | subject |
		| Goodbye | World   |
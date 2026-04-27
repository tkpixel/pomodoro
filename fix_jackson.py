import re

with open('pom.xml', 'r') as f:
    text = f.read()

text = re.sub(
    r'<artifactId>micronaut-http-client</artifactId>\n        </dependency>',
    '''<artifactId>micronaut-http-client</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micronaut</groupId>
            <artifactId>micronaut-jackson-databind</artifactId>
        </dependency>''',
    text
)

with open('pom.xml', 'w') as f:
    f.write(text)

import re

with open('src/main/java/module-info.java', 'r') as f:
    text = f.read()

text = re.sub(
    r'requires org\.reactivestreams;\n',
    '''requires org.reactivestreams;
    requires io.micronaut.micronaut_jackson_databind;
    requires io.micronaut.micronaut_json_core;
''',
    text
)

with open('src/main/java/module-info.java', 'w') as f:
    f.write(text)

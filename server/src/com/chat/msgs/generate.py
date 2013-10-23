import xml.etree.ElementTree as ET
from Cheetah.Template import Template

version = 'v1';
package = 'com.chat.msgs.v1'

tree = ET.parse(version + '.xml')
root = tree.getroot()

class Msg:
   def __init__(self, name):
       self.name = name
       self.fields = []
       self.fieldNames = []

class Field:
   def __init__(self, type, name):
       self.name = name
       self.type = type

t = Template(file='Message.java.template')

for msg in root:
   if msg.tag == 'msg':
      message = Msg(msg.attrib['name'])
      for field in msg:
          fld = Field(field.attrib['type'], field.attrib['name'])
          message.fields.append(fld)
          message.fieldNames.append(fld.type + " " + fld.name)
      t.msg = message
      t.package = package
      f = file(version + '/' + message.name + 'Message.java', 'w')
      f.write(str(t))

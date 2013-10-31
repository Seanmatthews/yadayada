import xml.etree.ElementTree as ET
from Cheetah.Template import Template

versionNum = 1
version = 'v' + str(versionNum);
package = 'com.chat.msgs.' + version
serverPath = "../java/src/com/chat/msgs/" + version
iosPath = "../ios/chatter/chatter/"

tree = ET.parse(version + '.xml')
root = tree.getroot()

typeLen = {
    'byte': '1',
    'short': '2',
    'int': '4',
    'long': '8'
}

typeMap = {
    'byte': 'Byte',
    'short': 'short',
    'int': 'int',
    'long': 'long long',
    'String': 'NSString*'
}

class Msg:
   def __init__(self, name, val):
       self.name = name
       self.val = val
       self.fields = []
       self.fieldNames = []
       self.fieldTypeNames = []

class Field:
   def __init__(self, type, name):
       self.name = name
       self.nameCap = name[0].capitalize() + name[1:]
       self.type = type

t = Template(file='Message.java.template')

clientMessages = []
serverMessages = []
for msg in root:
   if msg.tag == 'msg':
      message = Msg(msg.attrib['name'], msg.attrib['val'])
      len = '1';
      for field in msg:
          fld = Field(field.attrib['type'], field.attrib['name'])
          message.fields.append(fld)
          message.fieldTypeNames.append(fld.type + " " + fld.name)
          message.fieldNames.append(fld.name)

          if fld.type in typeLen:
              fieldLen = typeLen[fld.type];
          else:
              fieldLen = 'getStrLen(msg.get' + fld.nameCap + '())'
          len = len + ' + ' + fieldLen

      message.length = len

      t.msg = message
      t.package = package
      f = file(serverPath + '/' + message.name + 'Message.java', 'w')
      f.write(str(t))
      f.close()
  
      if msg.attrib['origin'] == 'client':
          clientMessages.append(message);
      else:
          serverMessages.append(message);

typesT = Template(file='MessageTypes.java.template')
typesT.msgs = clientMessages + serverMessages
typesT.package = package
f = file(serverPath + '/MessageTypes.java', 'w')
f.write(str(typesT))
f.close()

iosMessagesHT = Template(file='Messages.h.template')
iosMessagesHT.msgs = clientMessages + serverMessages
for msg in iosMessagesHT.msgs:
    for field in msg.fields:
        field.type = typeMap[field.type]
iosMessagesHT.apiVersion = versionNum
f = file(iosPath + 'Messages.h', 'w')
f.write(str(iosMessagesHT))
f.close()

iosMessagesMT = Template(file='Messages.m.template')
iosMessagesMT.msgs = iosMessagesHT.msgs
iosMessagesMT.apiVersion = versionNum
f = file(iosPath + 'Messages.m', 'w')
f.write(str(iosMessagesMT))
f.close() 

iosMessageUtilsT = Template(file='MessageUtils.m.template')
iosMessageUtilsT.msgs = iosMessagesHT.msgs
iosMessageUtilsT.apiVersion = versionNum
f = file(iosPath + 'MessageUtils.m', 'w')
f.write(str(iosMessageUtilsT))
f.close() 

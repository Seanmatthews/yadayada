import xml.etree.ElementTree as ET
from Cheetah.Template import Template

versionNum = 1
version = 'v' + str(versionNum);
package = 'com.chat.msgs.v1'

tree = ET.parse(version + '.xml')
root = tree.getroot()

typeLen = {
    'byte': '1',
    'short': '2',
    'int': '4',
    'long': '8'
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
      f = file(version + '/' + message.name + 'Message.java', 'w')
      f.write(str(t))
      f.close()
  
      if msg.attrib['origin'] == 'client':
          clientMessages.append(message);
      else:
          serverMessages.append(message);

connT = Template(file='Connection.java.template')
connImplT = Template(file='ConnectionImpl.java.template')

connT.recvMsgs = clientMessages
connT.sendMsgs = serverMessages
connT.package = package
connT.clientOrServer = 'Client'
f = file(version + '/ClientConnection.java', 'w')
f.write(str(connT))
f.close()

connImplT.recvMsgs = clientMessages
connImplT.sendMsgs = serverMessages
connImplT.package = package
connImplT.clientOrServer = 'Client'
f = file(version + '/ClientConnectionImpl.java', 'w')
f.write(str(connImplT))
f.close()

connT.sendMsgs = clientMessages
connT.recvMsgs = serverMessages
connT.clientOrServer = 'Server'
f = file(version + '/ServerConnection.java', 'w')
f.write(str(connT))
f.close()

connImplT.sendMsgs = clientMessages
connImplT.recvMsgs = serverMessages
connImplT.clientOrServer = 'Server'
f = file(version + '/ServerConnectionImpl.java', 'w')
f.write(str(connImplT))
f.close()

typesT = Template(file='MessageTypes.java.template')
typesT.msgs = clientMessages + serverMessages
typesT.package = package
f = file(version + '/MessageTypes.java', 'w')
f.write(str(typesT))
f.close()

typesT = Template(file='MessageTypes.h')
typesT.msgs = clientMessages + serverMessages
typesT.apiVersion = versionNum
f = file(version + '/MessageTypes.h', 'w')
f.write(str(typesT))
f.close() 

parserT = Template(file='Parser.c')
parserT.msgs = serverMessages
f = file(version + '/Parser.c', 'w')
f.write(str(parserT))
f.close() 

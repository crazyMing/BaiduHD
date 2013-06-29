import xml.dom.minidom
import sys
import codecs


if __name__=='__main__':
    MainVersion = '255.255.255'
    SubVersion = '255'
    versionCode = 0
    if len( sys.argv ) >= 3:
        MainVersion = sys.argv[1]
        SubVersion = sys.argv[2]
    Version = MainVersion+'.'+SubVersion
    print Version

    items=Version.split('.')
    if len(items)>=4:
        versionCode = long(long(items[0])*256*256*256+long(items[1])*256*256+long(items[2])*256+long(items[3]))
    print versionCode

    strversionCode = u'%d'% (versionCode)

    try:
        dom = xml.dom.minidom.parse('AndroidManifest.xml')
        root = dom.documentElement
        root.setAttribute('android:versionName', Version)
        root.setAttribute('android:versionCode', strversionCode)
    except:
        print "write version error"
        
    f = file('AndroidManifest.xml', 'wb')
    writer = codecs.lookup('utf-8')[3](f)
    dom.writexml(writer, encoding='utf-8')
    writer.close()


    

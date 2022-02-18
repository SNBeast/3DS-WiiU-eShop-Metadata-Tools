SRCDIR		:= src
CLASSDIR	:= class

SOURCES		:= $(foreach dir,$(SRCDIR),$(wildcard $(dir)/*.java))
CLASSES		:= $(patsubst $(SRCDIR)/%.java,$(CLASSDIR)/$(SRCDIR)/%.class,$(SOURCES))
OUT			:= 3DS-WiiU-eShop-Metadata-Tools.jar
MANIFEST	:= manifest.txt
JAVAC		:= javac
JAR			:= jar

all: $(CLASSES)
	$(JAR) cfm $(OUT) $(MANIFEST) -C $(CLASSDIR) $(SRCDIR)

$(CLASSDIR)/$(SRCDIR)/%.class: $(SRCDIR)/%.java | $(CLASSDIR)
	$(JAVAC) -d $(CLASSDIR) $<

$(CLASSDIR):
	mkdir -p $@

clean:
	rm -rf $(CLASSDIR) $(OUT)

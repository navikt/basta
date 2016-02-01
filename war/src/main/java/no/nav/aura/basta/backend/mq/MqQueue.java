package no.nav.aura.basta.backend.mq;

public class MqQueue {

    private static final int QUEUENAME_MAX_LENGTH = 44;
    private String name;
    private int maxSizeMb ; // max 100
    private int maxDepth;  
    private String description;
    private String alias;
    private String boqName;
    private int backoutThreshold;
    
    public MqQueue() {
    }
    
    public MqQueue(String queueName, int maxSizeMb, int maxDepth, String description) {
        this.name = queueName.toUpperCase();
        this.maxSizeMb = maxSizeMb;
        this.maxDepth = maxDepth;
        this.description = description;
        this.alias = "QA."+this.name;
        this.boqName = this.name+".BOQ";
        this.backoutThreshold = 1;
    }

     public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxSizeMb() {
		return maxSizeMb;
	}

	public void setMaxSizeMb(int maxSizeMb) {
		this.maxSizeMb = maxSizeMb;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getBoqName() {
		return boqName;
	}

	public void setBoqName(String boqName) {
		this.boqName = boqName;
	}

	public int getBackoutThreshold() {
		return backoutThreshold;
	}

	public void setBackoutThreshold(int backoutThreshold) {
		this.backoutThreshold = backoutThreshold;
	}

	public boolean isValidQueueName() {
        return name.length() <= QUEUENAME_MAX_LENGTH;
    }

    public void setMaxSizeInBytes(int bytes) {
        setMaxSizeMb(bytes/1024/1024);
    }

    public int getMaxSizeInBytes() {
        return getMaxSizeMb()*1024*1024;
    }

}

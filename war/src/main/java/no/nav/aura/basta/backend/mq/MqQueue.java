package no.nav.aura.basta.backend.mq;

public class MqQueue {

    private String name;
    private int maxSizeMb ; // max 100
    private int maxDepth;  
    private String description;
    private String alias;
    private String boqName;
    private int backoutThreshold=1;
    private boolean createBoq;
    
    public MqQueue() {
    }
    
    public MqQueue(String queueName, int maxSizeMb, int maxDepth, String description) {
        this.name = queueName.toUpperCase();
        this.maxSizeMb = maxSizeMb;
        this.maxDepth = maxDepth;
        this.description = description;
        this.alias = "QA."+this.name;
        this.boqName = this.name+".BOQ";
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

	private String getBoqName() {
		return boqName;
	}

	public void setBoqName(String boqName) {
		this.boqName = boqName;
	}
	
	public MqQueue getBackoutQueue(){
	    MqQueue backoutQueue = new MqQueue();
        backoutQueue.setName(this.getBoqName());
        backoutQueue.setDescription(this.getName() + " backout queue");
        backoutQueue.setMaxDepth(this.getMaxDepth());
        backoutQueue.setMaxSizeInBytes(this.getMaxSizeInBytes());
        backoutQueue.setCreateBackoutQueue(false);
        return backoutQueue;
	}

	public int getBackoutThreshold() {
		return backoutThreshold;
	}

	public void setBackoutThreshold(int backoutThreshold) {
		this.backoutThreshold = backoutThreshold;
	}


    public void setMaxSizeInBytes(int bytes) {
        setMaxSizeMb(bytes/1024/1024);
    }

    public int getMaxSizeInBytes() {
        return getMaxSizeMb()*1024*1024;
    }

    public boolean shouldCreateBoq() {
        return createBoq;
    }

    public void setCreateBackoutQueue(boolean createBoq) {
        this.createBoq = createBoq;
    }

}

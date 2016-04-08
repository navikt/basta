package no.nav.aura.basta.backend.mq;

import java.util.Optional;

public class MqQueue {

    private String name;
    private int maxSizeMb ; // max 100
    private int maxDepth;  
    private String description;
    private String alias;
    private String boqName;
    private int backoutThreshold;
    private boolean createBoq;
    private String clusterName;
    private int currentQueueDepth;
    
    public MqQueue() {
    }
    
    public MqQueue(String queueName, int maxSizeMb, int maxDepth, String description) {
        this.name = queueName.toUpperCase();
        this.maxSizeMb = maxSizeMb;
        this.maxDepth = maxDepth;
        this.description = description;
        this.alias = "QA."+this.name;
        this.backoutThreshold=1;
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

	public String getBackoutQueueName() {
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
    
    public Optional<String> getClusterName(){
      return Optional.ofNullable(clusterName);  
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getCurrentQueueDepth() {
        return currentQueueDepth;
    }

    public void setCurrentQueueDepth(int currentQueueDepth) {
        this.currentQueueDepth = currentQueueDepth;
    }

    @Override
    public String toString() {
        return "MqQueue [name=" + name + ", alias=" + alias + ", boqName=" + boqName + "]";
    }


}

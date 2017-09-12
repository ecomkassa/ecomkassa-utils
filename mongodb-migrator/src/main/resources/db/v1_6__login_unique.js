//mongeez formatted javascript
//changeset unlocker:v1_6
db.silhouette.user.ensureIndex({
        "loginInfo.providerKey" : 1,
        "loginInfo.providerID" : 1
	}, {
	    "unique" : true
    }
);

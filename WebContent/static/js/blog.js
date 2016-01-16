var bid=request("bid");

$(document).ready(function() {
	
	BlogManager.getBlog(bid, true, function(blog) {
		if(blog==null) {
			location.href="urlError.html";
			return;
		}
		fillText({
			"blog-date": blog.date.format(DATE_HOUR_MINUTE_FORMAT),
			"blog-title": blog.title,
			"blog-readers": blog.readers
		});
		$("#blog-content").html(blog.content);
		$("#loading-blog").hide();
	});

	CommentManager.getCommentsByBid(bid, function(comments) {
		if(comments.length>0) {
			$("#no-comment").hide();
		}
		for(var i in comments) {
			$("#comment-list").mengular(".comment-list-template", {
				cid: comments[i].cid,
				name: comments[i].name==""? "Anonymity": comments[i].name,
				date: comments[i].date.format(DATE_HOUR_MINUTE_FORMAT),
				content: comments[i].content,
			});
		}
	});

	$("#add-comment-clear").click(function() {
		$("#add-comment-name").val("");
		$("#add-comment-content").val("");
	});

	$("#add-comment-submit").click(function() {
		var name=$("#add-comment-name").val();
		var content=$("#add-comment-content").val();
		if(content=="") {
			$.messager.popup("Wirte some comment, please!");
			return;
		}
		CommentManager.addComment(bid, name, content, function() {
			$.messager.popup("Add Comment Success!");
			setTimeout(function() {
				location.href="blog.html?bid="+bid;
			}, 1000);
		});
	});

});
function deleteActivity(obj) {
  var activity = $(obj.target).closest("article");
  var id = activity.attr("id");

  $.ajax({url: '/posts/' + id,
          type: 'DELETE',
          success: function (data) {
            console.log (data);
            activity.hide();
          },
          error: function (request, status) {
            console.log(request);
          }});
  return false;
}

  // console.log("loaded")

$(function () {
  $(".delete-activity").live("click", deleteActivity);

  // alert("loaded");
  console.log("loaded")
  var ws = new WebSocket("ws://beta.jiksnu.com:8082/main/events");

  // console.log(ws.readyState);

  ws.onopen = function() {
    console.log("Socket has been opened");
    ws.send("foobar");
  }

  ws.onmessage = function(msg) {
    console.log(msg.data);
    $(".activities").append(msg.data);
  }

  // console.log(ws.readyState);

  // ws.send("foo")

  // console.log(ws.readyState);
})

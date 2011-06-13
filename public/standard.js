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

$(function () {
  $(".delete-activity").live("click", deleteActivity);
  $(".add-buttons li").show();
  $(".tag-line").hide();
  $(".recipients-line").hide();
  $(".location-line").hide();
  $(".add-tags").live("click", function(obj) {
    $(".tag-line").show();
  });
  $(".add-recipients").live("click", function(obj) {
    $(".recipients-line").show();
  });
  $(".add-location").live("click", function(obj) {
    $(".location-line").show();
  });

  var ws = new WebSocket("ws://beta.jiksnu.com:8082/main/events");

  ws.onopen = function() {
    console.log("Socket has been opened");
    ws.send("foobar");
  }

  ws.onmessage = function(msg) {
    console.log(msg.data);
    $(".activities").prepend(msg.data);
  }

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function(position) {
      console.log("lat: " + position.coords.latitude);
      $("input[name='lat']").val(position.coords.latitude);
      console.log("long: " + position.coords.longitude);
      $("input[name='long']").val(position.coords.longitude);
    });
  } else {
    console.log("location not available");
  }
});

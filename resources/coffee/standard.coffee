deleteActivity = (obj) ->
    activity = $(obj.target).closest("article")
    id = activity.attr("id")

    options =
        url: '/posts/' + id,
        type: 'DELETE',
        success: (data) ->
            console.log(data)
            activity.hide()
        error: (request, status) ->
            console.log(request)
    $.ajax(options)
    return false

positionHandler = (position) ->
    console.log("lat: " + position.coords.latitude)
    $("input[name='lat']").val(position.coords.latitude)
    console.log("long: " + position.coords.longitude)
    $("input[name='long']").val(position.coords.longitude)

initFn = () ->
    $(".delete-activity").live("click", deleteActivity)
    $(".add-buttons li").show()
    $(".tag-line").hide()
    $(".recipients-line").hide()
    $(".location-line").hide()

    ws = new WebSocket("ws://beta.jiksnu.com:8082/main/events")

    tagHandler = (obj) ->
        $(".tag-line").show()

    recipientHandler = (obj) ->
        $(".recipient-line").show()

    locationHandler = (obj) ->
        $(".location-line").show()

    $(".add-tags").live("click", tagHandler)
    $(".add-recipients").live("click", recipientHandler)
    $(".add-location").live("click", locationHandler)

    ws.onopen = () ->
        console.log("Socket has been opened")
        ws.send("foobar")

    ws.onmessage = (msg) ->
        console.log(msg.data)
        $(".activities").prepend(msg.data)

    if navigator.geolocation
        navigator.geolocation.getCurrentPosition(positionHandler)
    else
        console.log("location not available")

$(initFn)

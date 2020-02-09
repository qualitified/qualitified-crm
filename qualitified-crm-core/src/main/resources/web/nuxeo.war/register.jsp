<!DOCTYPE html>
<!-- Nuxeo Enterprise Platform -->
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ page language="java"%>
<%@ page import="org.nuxeo.runtime.api.Framework"%>
<%
String base64 = Framework.getProperty("ccm.register.base64");
%>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Register</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>
    <style>
  .bs-example{
    margin-top: 80px;
    margin-left: 400px;
    margin-right: 400px;
  }
</style>
    <script>
  $( document ).ready(function() {
    $( "#form" ).submit(function( event ) {
      event.preventDefault();
      $.ajax({
        type: "post",
        url: "/nuxeo/site/api/v1/automation/Document.Create",
        contentType:"application/json; charset=utf-8",
        dataType: "json",
        data: "{"+
                "\"params\": {"+
                   "\"type\":\"Silhouette\","+
                   "\"name\":\"silhouette\","+
                   "\"properties\":\"dc:title="+$("#inputFirstName").val()+" "+$("#inputLastName").val()+"\\n"+
                                  "person:firstName="+$("#inputFirstName").val()+"\\n"+
                                  "person:lastName="+$("#inputLastName").val()+"\\n"+
                                  "person:email="+$("#inputEmail").val()+"\\n"+
                                  "silhouette:company="+$("#inputCompany").val()+"\\n"+
                                  "custom:systemField1=Registration\""+
                "},"+
                "\"input\":\"/Marketing\","+
                "\"context\":{}"+
              "}",
          beforeSend: function (xhr){
              xhr.setRequestHeader('Authorization', 'Basic <%=base64%>');
              xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
          },
          error: function(returnval) {
            $("#error").show();
            $("#error").delay(2000).fadeOut(1000);
          },
          success: function (returnval) {
            $("#success").show();
            $("#inputFirstName").val("");
            $("#inputLastName").val("");
            $("#inputEmail").val("");
            $("#inputCompany").val("");
          }
      });
    });
  });
</script>
</head>
<body>
<div class="bs-example">
  <div id="formPlace">
    <div><img src="/nuxeo/img/screenshot.png"/></div>
    <br/>
    <form id="form">
        <div class="form-group">
            <!--label for="inputFirstName">First Name</label-->
            <input type="text" class="form-control" id="inputFirstName" placeholder="First Name" required>
        </div>
        <div class="form-group">
            <!--label for="inputLastName">Last Name</label-->
            <input type="text" class="form-control" id="inputLastName" placeholder="Last Name" required>
        </div>
        <div class="form-group">
            <!--label for="inputEmail">Email</label-->
            <input type="email" class="form-control" id="inputEmail" placeholder="Email" required>
        </div>
        <div class="form-group">
            <!--label for="inputCompany">Company</label-->
            <input type="text" class="form-control" id="inputCompany" placeholder="Company" required>
        </div>
        <div class="checkbox">
            <label><input type="checkbox" required> I accept <a href="/nuxeo/terms.html" target="_blank">Terms and Conditions</a></label>
        </div>
        <button id="submit" class="btn btn-primary">Register</button>
    </form>
  </div>
  <br/>
  <div id="success" style="display:none" class="alert alert-success alert-dismissible fade show">
    <strong>Success!</strong> You registration request was submitted successfully. <br/>Please checkout your email for your access.
    <button type="button" class="close" data-dismiss="alert">&times;</button>
  </div>
  <div id="error" style="display:none" class="alert alert-danger alert-dismissible fade show">
    <strong>Error!</strong> Something went wrong. Please try again.
    <button type="button" class="close" data-dismiss="alert">&times;</button>
  </div>
</div>
</body>
</html>

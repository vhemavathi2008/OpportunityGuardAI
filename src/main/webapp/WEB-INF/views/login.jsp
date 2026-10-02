<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Log in · OpportunityGuard AI</title>
  <link rel="stylesheet" href="${ctx}/css/style.css">
</head>

<body>

<div class="auth">

  <section class="auth-side">

    <h1>OpportunityGuard AI</h1>

    <p>Never miss your next opportunity.</p>

    <p style="font-size:16px">
      Track hackathons, internships and scholarships from your own inbox,
      check how trustworthy they look, and see exactly which stage you have reached.
    </p>

  </section>


  <section class="auth-form">

    <div class="auth-box">

      <h2>Log in</h2>

      <p class="muted" style="margin:0 0 20px">
        Use the account you registered with.
      </p>

      <c:if test="${not empty flashSuccess}">
        <div class="alert ok" role="status">
          <c:out value="${flashSuccess}"/>
        </div>
      </c:if>

      <c:if test="${not empty flashError}">
        <div class="alert err" role="alert">
          <c:out value="${flashError}"/>
        </div>
      </c:if>

      <c:if test="${not empty error}">
        <div class="alert err" role="alert">
          <c:out value="${error}"/>
        </div>
      </c:if>


      <form method="post" action="${ctx}/login" novalidate>

        <input type="hidden"
               name="_csrf"
               value="${csrfToken}">

        <div class="field">

          <label for="email">
            Email
          </label>

          <input id="email"
                 type="email"
                 name="email"
                 value="<c:out value='${email}'/>"
                 autocomplete="username"
                 required
                 autofocus>

        </div>


        <div class="field">

          <label for="password">
            Password
          </label>

          <input id="password"
                 type="password"
                 name="password"
                 autocomplete="current-password"
                 required>

        </div>


        <button class="btn" type="submit">
          Log in
        </button>

      </form>


      <p class="auth-alt">
        New here?
        <a href="${ctx}/register">Create account</a>
      </p>

    </div>

  </section>

</div>

</body>
</html>
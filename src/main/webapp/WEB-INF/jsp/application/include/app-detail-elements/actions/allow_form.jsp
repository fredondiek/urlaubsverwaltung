<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:if test="${action == 'allow'}">
<script type="text/javascript">
    document.addEventListener('DOMContentLoaded', function() {
        $("#allow").show();
    })
</script>
</c:if>

<spring:url var="URL_PREFIX" value="/web"/>

<c:if test="${shortcut == true}">
    <c:set var="ACTION_URL" value="?redirect=/application/"/>
</c:if>

<c:choose>
    <c:when test="${shortcut == true}">
        <c:set var="ACTION_URL" value="${URL_PREFIX}/application/${application.id}/allow?redirect=/web/application/"/>
    </c:when>
    <c:otherwise>
        <c:set var="ACTION_URL" value="${URL_PREFIX}/application/${application.id}/allow"/>
    </c:otherwise>
</c:choose>

<form:form id="allow" cssClass="form action-form confirm alert alert-success" method="POST" action="${ACTION_URL}" modelAttribute="comment">
    <div class="form-group">
        <c:choose>
            <c:when test="${isDepartmentHeadOfPerson && !isSecondStageAuthorityOfPerson && application.twoStageApproval && application.status == 'WAITING'}">
                <strong class="tw-font-medium"><spring:message code='action.temporary_allow.confirm'/></strong>
            </c:when>
            <c:otherwise>
                <strong class="tw-font-medium"><spring:message code='action.allow.confirm'/></strong>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="form-group">
        <div class="tw-text-sm">
            <spring:message code="action.comment.optional"/>: (<span
            id="text-confirm"></span><spring:message code="action.comment.maxChars"/>)
        </div>
        <form:textarea rows="2" cssClass="form-control" cssErrorClass="form-control error" path="text"
                       onkeyup="count(this.value, 'text-confirm');"
                       onkeydown="maxChars(this,200); count(this.value, 'text-confirm');"/>
    </div>

    <div class="form-group is-sticky row">
        <button type="submit" class="button-main-green col-xs-12 col-sm-5">
            <c:choose>
                <c:when test="${isDepartmentHeadOfPerson && !isSecondStageAuthorityOfPerson && application.twoStageApproval && application.status == 'WAITING'}">
                    <spring:message code='action.temporary_allow'/>
                </c:when>
                <c:otherwise>
                    <spring:message code='action.allow'/>
                </c:otherwise>
            </c:choose>
        </button>
        <button type="button" class="button col-xs-12 col-sm-5 pull-right" onclick="$('#allow').hide();">
            <spring:message code="action.cancel"/>
        </button>
    </div>

</form:form>

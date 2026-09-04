package com.docstencil.core.render

import com.docstencil.core.parser.TemplateParser
import com.docstencil.core.parser.model.ExpressionStmt
import com.docstencil.core.parser.model.GetExpr
import com.docstencil.core.parser.model.OptionalGetExpr
import com.docstencil.core.parser.model.VariableExpr
import com.docstencil.core.scanner.model.ContentIdx
import com.docstencil.core.scanner.model.TemplateToken
import com.docstencil.core.scanner.model.TemplateTokenType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyAccessTest {

    @Test
    fun `parser should support string literals as property names`() {
        val parser = TemplateParser()
        // Simulate tokens for: {Person."Left Arm"}
        val tokens = listOf(
            TemplateToken(TemplateTokenType.DELIMITER_OPEN, "{", null, null, ContentIdx(0)),
            TemplateToken(TemplateTokenType.IDENTIFIER, "Person", null, null, ContentIdx(1)),
            TemplateToken(TemplateTokenType.DOT, ".", null, null, ContentIdx(7)),
            TemplateToken(TemplateTokenType.STRING, "\"Left Arm\"", null, "Left Arm", ContentIdx(8)),
            TemplateToken(TemplateTokenType.DELIMITER_CLOSE, "}", null, null, ContentIdx(18))
        )

        val stmts = parser.parse(tokens)
        assertEquals(1, stmts.size, "Should have parsed exactly one statement")
        
        val exprStmt = stmts[0] as ExpressionStmt
        val getExpr = exprStmt.expr as GetExpr

        assertTrue(getExpr.obj is VariableExpr, "Target should be a VariableExpr")
        assertEquals("Person", (getExpr.obj).name.lexeme)
        assertEquals(TemplateTokenType.STRING, getExpr.name.type)
        assertEquals("Left Arm", getExpr.name.literal as String)
    }

    @Test
    fun `PropertyAccessHelper should resolve property from string literal token for maps`() {
        val helper = PropertyAccessHelper()
        val data = mapOf("Left Arm" to "Bionic")
        
        // Create a token representing the "Left Arm" property
        val token = TemplateToken(TemplateTokenType.STRING, "\"Left Arm\"", null, "Left Arm", ContentIdx(0))

        val result = helper.getProperty(data, token)
        assertEquals("Bionic", result)
    }

    @Test
    fun `PropertyAccessHelper should handle missing properties with string literals`() {
        val helper = PropertyAccessHelper()
        val data = mapOf("Leg" to "Wooden")
        val token = TemplateToken(TemplateTokenType.STRING, "\"Left Arm\"", null, "Left Arm", ContentIdx(0))

        // Should throw RuntimeError because "Left Arm" is missing in the map
        val exception = org.junit.jupiter.api.assertThrows<com.docstencil.core.error.TemplaterException.RuntimeError> {
            helper.getProperty(data, token)
        }
        assertTrue(exception.message!!.contains("Property or key 'Left Arm' not found"))
    }
}
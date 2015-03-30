it("Basic test", function()
    assert.are_not.equals("hello", "world")
end)

describe("Another group", function()
	it("Child test", function()
        assert.are_not.is_not.has_not.equals("hello", "world")
    end)
end)

pending("Pending test", function()
	error("This should not be called")
end)

local called = 0
before_each(function()
	called = called + 1
end)

teardown(function()
	assert.equals(2, called)
end)

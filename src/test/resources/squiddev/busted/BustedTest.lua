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

it("Spy", function()
	a = spy.new(function() end)

	a(1, 2, 3)
	assert.spy(a).was_not.called_with(1)
	assert.spy(a).was.called_with(1, 2, 3)
end)

local called = 0
before_each(function()
	called = called + 1
end)

teardown(function()
	assert.equals(3, called)
end)

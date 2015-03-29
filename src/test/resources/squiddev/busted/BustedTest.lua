it("Basic test", function()
    print("HELLO")
end)

define("Another group", function()
	it("Child test", function()
        print("Hai!")
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
	print("Called " .. called)
end)

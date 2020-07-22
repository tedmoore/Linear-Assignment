Auction {
	//https://github.com/kylemcdonald/AuctionAlgorithm/blob/master/cpp/auction.cpp

	var assignment,
	prices,
	epsilon,
	costMatrix,
	iter;

	*new {
		^super.new.init;
	}

	init {
		// cout << "Please enter a problem size: ";
		// int probSize;
		// cin >> probSize;
		// auction(probSize);
		// return 0;
	}

	fitDataToRect {
		arg corpus;
		var available_spots, cost_matrix, assignments, rows, cols, n, rowsPad, colsPad;

		n = corpus.size;

		"n: %".format(n).postln;

		cols = n.sqrt.asInteger;
		rows = n / cols;



		if(((rows - rows.asInteger) == 0).not,{
			//"adding".postln;
			rows = rows + 1;
		});
		rows = rows.asInteger;

		"rows, cols: % , %".format(rows, cols).postln;

		available_spots = Array.fill(rows,{
			arg y;
			y = y.linlin(0,rows-1,0.0,1.0);
			Array.fill(cols,{
				arg x;
				x = x.linlin(0,cols-1,0.0,1.0);
				[x,y];
			});
		}).flatten;

		"available spots: %".format(available_spots).postln;

		cost_matrix = corpus.collect({
			arg pt, i;
			//"pt: %".format(pt).postln;
			available_spots.collect({
				arg as;
				"% pt, as: % , % --- %".format(i,pt,as,i/corpus.size).postln;
				(as - pt).pow(2).sum.sqrt;
			});
		});

		"cost matrix: %".format(cost_matrix).postln;
		cost_matrix.dopostln;

		assignments = Auction().auction(1.0 / cost_matrix);
		"assignments: %".format(assignments).postln;
		assignments = assignments.collect({
			arg assign;
			available_spots[assign];
		});
		"assignments: %".format(assignments).postln;
		^assignments;
	}

	auction {
		arg costMatrix_;
		var /*assignment, prices, epsilon, iter, */problemSize;
		costMatrix = costMatrix_;

		problemSize = costMatrix.size;

		assignment = Array.fill(problemSize,{inf});
		prices = Array.fill(problemSize,{1});
		epsilon = 1.0;
		iter = 1;

		while({epsilon > (1.0/problemSize)},{
			assignment = Array.fill(problemSize,{inf});
			while({
				assignment.includes(inf);
			},
			{
				"auction round: %".format(iter).postln;
				iter = iter + 1;
				this.auctionRound(/*assignment, prices, *//*costMatrix*//*, epsilon*/);

			});
			epsilon = epsilon * 0.25;
		});

		^assignment;
	}

	auctionRound {
		//arg /*assignment, prices, *//*costMatrix*//*, epsilon*/;
		var n, tmpBidded, tmpBids, unAssig;

		n = prices.size;

		/*
		These are meant to be kept in correspondance such that bidded[i]
		and bids[i] correspond to person i bidding for bidded[i] with bid bids[i]
		*/
		tmpBidded = List.new;
		tmpBids = List.new;
		unAssig = List.new;

		/* Compute the bids of each unassigned individual and store them in temp */
		assignment.size.do({
			arg i;
			//"--i: %".format(i).postln;
			if(assignment[i] == inf,{
				var optValForI, secOptValForI, optObjForI, secOptObjForI, bidForI;
				//"adding i: %".format(i).postln;
				unAssig.add(i);

				/*
				Need the best and second best value of each object to this person
				where value is calculated row_{j} - prices{j}
				*/
				optValForI = -inf;
				secOptValForI = -inf;
				n.do({
					arg j;
					var curVal;
					/*					"costMatrix: %".format(costMatrix).postln;
					"prices:     %".format(prices).postln;
					"i:          %".format(i).postln;
					"j:          %".format(j).postln;*/
					curVal = costMatrix[i][j] - prices[j];
					if (curVal > optValForI,{
						secOptValForI = optValForI;
						secOptObjForI = optObjForI;
						optValForI = curVal;
						optObjForI = j;
					},{
						if(curVal > secOptValForI,{
							secOptValForI = curVal;
							secOptObjForI = j;
						});

					});
				});

				/* Computes the highest reasonable bid for the best object for this person */
				bidForI = optValForI - secOptValForI + epsilon;

				/*				"bidForI:   %".format(bidForI).postln;
				"tmpBidded: %".format(tmpBidded).postln;
				"tmpBids:   %".format(tmpBids).postln;*/
				/* Stores the bidding info for future use */
				tmpBidded.add(optObjForI);
				tmpBids.add(bidForI);
			});
		});

		/*
		Each object which has received a bid determines the highest bidder and
		updates its price accordingly
		*/
		n.do({
			arg j;
			var indices;
			indices = this.getIndicesWithVal(tmpBidded, j);
			//"IndicesWithVal: %".format(indices).postln;
			if(indices.size != 0,{
				var i_j, highestBidForJ, break;
				/* Need the highest bid for object j */
				highestBidForJ = -inf;
				indices.size.do({
					arg i;
					var curVal = tmpBids[indices[i]];
					if(curVal > highestBidForJ,{
						//"setting new highest bid".postln;
						highestBidForJ = curVal;
						i_j = indices[i];
						//"curVal:         %".format(curVal).postln;
						//"highestBidForJ: %".format(highestBidForJ).postln;
						//"i_j:            %".format(i_j).postln;
					});
				});

				/* Find the other person who has object j and make them unassigned */
				break = false;
				assignment.size.do({
					arg i;
					if(break.not,{
						//"i: %".format(i).postln;
						if(assignment[i] == j,{
							assignment[i] = inf;
							break = true;
						});
					});
				});

				/* Assign oobject j to i_j and update the price vector */
				/*				"unAssig:      %".format(unAssig).postln;
				"i_j:          %".format(i_j).postln;
				"unAssig[i_j]: %".format(unAssig[i_j]).postln;*/
				assignment[unAssig[i_j]] = j;
				prices[j] = prices[j] + highestBidForJ;
			});
		});
	}


	/*<--------------------------------------   Utility Functions   -------------------------------------->*/

	/*	vector<int> makeRandC(int size)
	{
	srand (time(NULL));
	vector<int> mat(size * size, 2);
	for(int i = 0; i < size; i++)
	{
	for(int j = 0; j < size; j++)
	{
	mat[i + j * size] = rand() % size + 1;
	}
	}
	return mat;
	}

	void printMatrix(vector<cost_t>* mat, int size)
	{
	for(int i = 0; i < size; i++)
	{
	for(int j = 0; j < size; j++)
	{
	cout << mat->at(i + j * size) << "\t";
	}
	cout << endl;
	}
	}

	template <class T>
	void printVec(vector<T>* v)
	{
	for(int i = 0; i < v->size(); i++)
	{
	if (v->at(i) == numeric_limits<T>::max())
	{
	cout << "INF" << "\t";
	}
	else
	{
	cout << v->at(i) << "\t";
	}
	}
	cout << endl;
	}*/

	/* Returns a vector of indices from v which have the specified value val */
	getIndicesWithVal {
		arg v, val;
		var out;
		out = List.new;
		v.size.do({
			arg i;
			if(v[i] == val,{
				out.add(i);
			})
		});
		^out;
	}

	/*	void reset(vector<cost_t>* v, cost_t val)
	{
	for (int i = 0; i < v->size(); i++)
	{
	v->at(i) = val;
	}
	}*/
}